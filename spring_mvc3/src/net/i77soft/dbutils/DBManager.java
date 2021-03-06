package net.i77soft.dbutils;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DBManager {

	private final static Log log = LogFactory.getLog(DBManager.class);
	private final static ThreadLocal<Connection> conns = new ThreadLocal<Connection>();
	private static Properties s_dbProperties = null;
	private static DataSource dataSource = null;
	private static boolean show_sql = false;

	static {
		//initDataSource(null);
	}

	/**
	 * 初始化连接池
	 * 
	 * @param props
	 * @param show_sql
	 */
	@SuppressWarnings("unused")
	private final static void initDataSource(Properties dbProperties) {
		initDataSource(null, dbProperties);
	}

	/**
	 * 初始化连接池
	 * 
	 * @param props
	 * @param show_sql
	 */
	public final static void initDataSource(ServletContext servletContext, Properties dbProperties) {
		try {
			if (dataSource != null)
				return;

			if (dbProperties == null) {
				if (s_dbProperties == null) {
					dbProperties = new Properties();
					if (dbProperties != null) {
						InputStream dbInputStream = null;
						//dbProperties.load(DBManager.class.getResourceAsStream("db.properties"));
						//dbProperties.load(DBManager.class.getClass().getClassLoader().getResourceAsStream("WEB-INF/db.properties"));
						//dbProperties.load(servlet.getServletConfig().getServletContext().getResourceAsStream("/WEB-INF/db.properties"));
						//dbProperties.load(httpSession.getServletContext().getResourceAsStream("/WEB-INF/db.properties"));
						if (servletContext != null)
							dbInputStream = servletContext.getResourceAsStream("/WEB-INF/db.properties");
						if (dbInputStream == null)
							dbInputStream = DBManager.class.getResourceAsStream("/db.properties");
						if (dbInputStream != null)
							dbProperties.load(dbInputStream);
						s_dbProperties = dbProperties;
					}
				}
				else
					dbProperties = s_dbProperties;
			}
			Properties cp_props = new Properties();
			for (Object key : dbProperties.keySet()) {
				String skey = (String) key;
				if (skey.startsWith("jdbc.")) {
					String name = skey.substring(5);
					cp_props.put(name, dbProperties.getProperty(skey));
					if ("show_sql".equalsIgnoreCase(name)) {
						show_sql = "true".equalsIgnoreCase(dbProperties.getProperty(skey));
					}
				}
			}
			dataSource = (DataSource) Class.forName(
					cp_props.getProperty("datasource")).newInstance();
			if (dataSource.getClass().getName().indexOf("c3p0") > 0) {
				// Disable JMX in C3P0
				System.setProperty(
						"com.mchange.v2.c3p0.management.ManagementCoordinator",
						"com.mchange.v2.c3p0.management.NullManagementCoordinator");
			}
			log.info("Using DataSource : " + dataSource.getClass().getName());
			BeanUtils.populate(dataSource, cp_props);

			Connection conn = getConnection();
			DatabaseMetaData mdm = conn.getMetaData();
			log.info("Connected to " + mdm.getDatabaseProductName() + " "
					+ mdm.getDatabaseProductVersion());
			closeConnection();
		} catch (Exception e) {
			throw new DBException(e);
		}
	}

	/**
	 * 断开连接池
	 */
	public final static void closeDataSource() {
		try {
			dataSource.getClass().getMethod("close").invoke(dataSource);
		} catch (NoSuchMethodException e) {
		} catch (Exception e) {
			log.error("Unabled to destroy DataSource!!! ", e);
		}
		finally {
			dataSource = null;
		}
	}

	/**
	 * 获取连接
	 * @return
	 * @throws SQLException
	 */
	public final static Connection getConnection() throws SQLException {
		Connection conn = conns.get();
		if (conn == null || conn.isClosed()) {
			conn = dataSource.getConnection();
			conns.set(conn);
		}
		return (show_sql && !Proxy.isProxyClass(conn.getClass())) ?
				new _DebugConnection(conn).getConnection() : conn;
	}

	/**
	 * 关闭连接
	 * @throws SQLException
	 */
	public final static void closeConnection() throws SQLException {
		Connection conn = conns.get();
		try {
			if (conn != null && !conn.isClosed()) {
				conn.setAutoCommit(true);
				conn.close();
			}
		} catch (SQLException e) {
			log.error("Unabled to close connection!!! ", e);
		}
		finally {
			conns.set(null);
		}
	}

	/**
	 * 用于跟踪执行的SQL语句
	 * 
	 * @author Winter Lau
	 */
	static class _DebugConnection implements InvocationHandler {

		private final static Log log = LogFactory.getLog(_DebugConnection.class);

		private Connection conn = null;

		public _DebugConnection(Connection conn) {
			this.conn = conn;
		}

		/**
		 * Returns the conn.
		 *
		 * @return Connection
		 */
		public Connection getConnection() {
			return (Connection) Proxy.newProxyInstance(conn.getClass()
					.getClassLoader(), conn.getClass().getInterfaces(), this);
		}

		@Override
		public Object invoke(Object proxy, Method m, Object[] args)
				throws Throwable {
			try {
				String method = m.getName();
				if ("prepareStatement".equals(method) || "createStatement".equals(method))
					log.info("[SQL] >>> " + args[0]);
				return m.invoke(conn, args);
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}

	}

}
