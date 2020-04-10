package moe.sannaha.googleexceltomysql.utils;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 使用c3p0
 *
 * 1.创建一个DataSource对象
 * 2.编写一个方法获取DataSource
 * 3.编写一个方法 获取连接
 */
public class JDBCUtils {
    // 1.创建一个DataSource对象
    private static DataSource dataSource = new ComboPooledDataSource();

    //2.编写一个方法获取DataSource
    public static DataSource getDataSource(){
        return dataSource;
    }

    // 3.编写一个方法 获取连接
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
