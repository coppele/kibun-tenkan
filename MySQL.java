package /* TODO パッケージ名をこちらに */;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * MySQLとつなげるあれです。
 */
public class MySQL {

    protected static final String URL = "jdbc:mysql//%s:%s/%s?allowPublicKeyRetrieval=true&useSSL=false";

    /**
     * urlとかの情報を直接打ち込むタイプのあれです。
     * @param url {@link MySQL#URL jdbc:mysql//%s:%s/%s?allowPublicKeyRetrieval=true&useSSL=false} が参考になるかもしれません
     * @param user ユーザーです。大体はrootです。
     * @param pass パスワードです。
     * @return {@link MySQL} インスタンスが帰ってきます。
     */
    public static MySQL of(String url, String user, String pass) {
        var mysql = new MySQL();
        mysql.url = url;
        mysql.user = user;
        mysql.pass = pass;
        mysql.connect(statement -> true);
        return mysql;
    }

    /**
     * config.ymlなどのYamlファイルから読み込むタイプのあれです。
     * @param config {@link org.bukkit.plugin.java.JavaPlugin#getConfig} など、{@link FileConfiguration} を入れてください。
     * @return {@link MySQL.Config} インスタンスが帰ってきます。
     */
    public static MySQL of(FileConfiguration config) {
        var mysql = new MySQL.Config();
        if (config.get("MySQL", null) instanceof ConfigurationSection section) try {
            mysql.reload(section);
            mysql.getConnection().close();
        } catch (Throwable e) {
            if (mysql.printLog) e.printStackTrace();
        }
        return mysql;
    }

    /**
     * reloadでconfigから読み込めるようにしただけです。それ以外は {@link MySQL} と同じです。
     */
    public static class Config extends MySQL {

        private boolean isLoad;

        /**
         * 再読み込みをします。<br>
         * {@link ConfigurationSection section} 内の Host、Port、DataBase、User、Pass を読み込みます。
         * @param section どこの値を読み込むかを指定します。{@link MySQL#of(FileConfiguration)} が参考になるかもしれません
         */
        public void reload(ConfigurationSection section) {
            this.isLoad = false;
            if (section.get("Host", null) instanceof String host && !host.isBlank()
                    && section.get("Port", null) instanceof String port && !port.isBlank()
                    && section.get("DataBase", null) instanceof String db && !db.isBlank()
                    && section.get("User", null) instanceof String user && !user.isBlank()
                    && section.get("Pass", null) instanceof String pass && !pass.isBlank()) {
                this.url = URL.formatted(host, pass, db);
                this.user = user;
                this.pass = pass;
                this.isLoad = true;
            }
        }

        @Override
        public boolean connect(SQLPredicate<Statement> predicate) {
            return isLoad && super.connect(predicate);
        }
    }

    public interface SQLPredicate<T> {
        boolean test(T t) throws SQLException;
    }

    // Instance ///////////////////////////////////////////////////////////////

    protected String url, user, pass;

    /**
     * エラーが発生した際にコンソールに出力するかを設定できます。
     */
    public boolean printLog = true;

    private MySQL() {}

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, new Properties() {{
            put("user", user);
            put("password", pass);
        }});
    }

    /**
     * 接続するだけならこちらが便利です。
     * @param predicate {@link Statement} を操作できます。こちらで戻り値をfolseにした場合、接続に成功していてもfalseが帰ってきます。
     * @return 成功したか否かで結果を返します。
     */
    public boolean connect(SQLPredicate<Statement> predicate) {
        try (var con = getConnection(); var st = con.createStatement()) {
            return predicate.test(st);
        } catch (Throwable e) {
            if (printLog) e.printStackTrace();
            return false;
        }
    }

    /**
     * 実行だけします。INSERT文などに使えます。
     * @param query クエリ文です。もし {@link Language} が使えない場合削除してあげてください。
     * @return 接続と実行に成功したか否かで結果を返します。
     */
    public boolean execute(@Language("sql") String query) {
        return query != null && connect(st -> st.execute(query));
    }
    
    /**
     * クエリを要求します。SELECT文などに使えます。
     * @param query クエリ文です。もし {@link Language} が使えない場合削除してあげてください。
     * @param predicate 取得した結果を操作します。こちらで戻り値をfolseにした場合、要求に成功していてもfalseが帰ってきます。
     * @return 接続と要求に成功したか否かで結果を返します。
     */
    public boolean query(@Language("sql") String query, SQLPredicate<ResultSet> predicate) {
        return predicate == null ? execute(query) : query != null && connect(st -> {
            try (var rs = st.executeQuery(query)) {
                return predicate.test(rs);
            }
        });
    }
}
