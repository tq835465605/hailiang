package hailiang.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCUtils {

	public static Connection getOrcaleConn(String ip,String databasename,String user,String password) {

		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			String url = "jdbc:oracle:thin:@"+ip+":1521:"+databasename;
			Connection con = DriverManager.getConnection(url, user, password);// 获取连接
			System.out.println("连接成功！");
			return con;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// 加载Oracle驱动程序
		return null;
	}
	
	public static ResultSet select(Connection conn, String sql, Object[] pras) {
		ResultSet resu = null;
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			if(pras!=null) {
				for (int i = 0; i < pras.length; i++) {
					ps.setObject(i + 1, pras[i]);
				}
			}
			resu = ps.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resu;
	}
	
	/**
	 * <p>Title: 关闭数据库结果集</p>
	 * <p>Description: </p>
	 * 
	 */
	public  static void closeResultSet(ResultSet resultSet) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * <p>Title: 关闭数据库连接</p>
	 * <p>Description: </p>
	 * 
	 */
	public  static void closeConnection(Connection tnconn) {
		try {
			if (tnconn != null) {
				tnconn.close();
			}
			System.out.println("数据库连接关闭");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws SQLException {
		Connection connection = getOrcaleConn("172.17.15.117", "ora11g", "ecology", "ecology001");
		System.out.println(connection);
		String sql = "select * from CQ_YZ";
		String st_wfid = "9,10";
		sql = "select ID,NODENAME from CQ_JDXZ  where WFID in ("+st_wfid+")";
		ResultSet resultSet=select(connection,sql,null);
		while(resultSet.next()) {
			String id = resultSet.getString("ID");
			String yzmc = resultSet.getString("NODENAME");
			System.out.print(id+" "+yzmc);
			System.out.println();
		}
		System.out.println(resultSet);
	}

}
