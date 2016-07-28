package common.toolkit.java.util.zkwatcher;

import common.toolkit.java.util.ObjectUtil;
import common.toolkit.java.util.PropertiesUtil;
import common.toolkit.java.util.StringUtil;
import common.toolkit.java.util.ThreadUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 *zookeeper watcher
 * zookeeper节点监控类
 * 
 */
public class PushOrPull implements Watcher {

	private AtomicInteger seq = new AtomicInteger();

	private static final Logger LOG = LoggerFactory.getLogger( PushOrPull.class );

	private static String ZK_PATH;
	private static final String LOG_PREFIX_OF_MAIN = "【Main】";
	private static String CONFIG_FILE;

	private ZooKeeper zk = null;

	private CountDownLatch connectedSemaphore = new CountDownLatch( 1 );

	public PushOrPull(String zkpath, String configfile){
           this.ZK_PATH = zkpath;
		   this.CONFIG_FILE = configfile;
	}

	/**
	 * 创建ZK连接
	 * 
	 * @param connectString
	 *            ZK服务器地址列表
	 * @param sessionTimeout
	 *            Session超时时间
	 */
	public void createConnection(String connectString, int sessionTimeout ) {
		this.releaseConnection();
		try {
			zk = new ZooKeeper( connectString, sessionTimeout, this );
			LOG.info( LOG_PREFIX_OF_MAIN + "开始连接ZK服务器" );
			LOG.info( LOG_PREFIX_OF_MAIN + "节点成功，path：" + ZK_PATH );
			connectedSemaphore.await();
		} catch ( Exception e ) {
		}
	}

	/**
	 * 关闭ZK连接
	 */
	public void releaseConnection() {
		if ( !ObjectUtil.isBlank( this.zk ) ) {
			try {
				this.zk.close();
			} catch ( InterruptedException e ) {
			}
		}
	}

	/**
	 * 创建节点
	 * 
	 * @param path
	 *            节点path
	 * @param data
	 *            初始数据内容
	 * @return
	 */
	public boolean createPath(String path, String data ) {
		try {
			LOG.info( LOG_PREFIX_OF_MAIN + "Create path success, Path: " + this.zk.create( path, //
					data.getBytes(), //
					Ids.OPEN_ACL_UNSAFE, //
					CreateMode.PERSISTENT ) + ", content: " + data );
		} catch ( Exception e ) {
		}
		return true;
	}

	/**
	 * 读取指定节点数据内容
	 * 
	 * @param path
	 *            节点path
	 * @return
	 */
	public String readData(String path, boolean needWatch ) {
		try {
			return new String( this.zk.getData( path, needWatch, null ) );
		} catch ( Exception e ) {
			return "";
		}
	}

	/**
	 * 更新指定节点
	 * 
	 * @param path
	 *            节点path
	 */
	public void updateData(String path, String data ) {
		try {
			LOG.info( LOG_PREFIX_OF_MAIN + "Update data success:" + this.zk.setData( path, data.getBytes(), -1 )  );
		} catch ( Exception e ) {
			// TODO
		}
	}
	public void deleteAllTestPath(){
		this.deleteNode( ZK_PATH );
	}
	/**
	 * 删除指定节点
	 * @param path 节点path
	 */
	public void deleteNode( String path ) {
		try {
			this.zk.delete( path, -1 );
			LOG.info( LOG_PREFIX_OF_MAIN + "删除节点成功，path：" + path );
		} catch ( Exception e ) {
			//TODO
		}
	}

	/**
	 * 收到来自Server的Watcher通知后的处理。
	 */
	@Override
	public void process( WatchedEvent event ) {

		ThreadUtil.sleep( 200 );
		if ( ObjectUtil.isBlank( event ) ) {
			return;
		}
		// 连接状态
		KeeperState keeperState = event.getState();
		// 事件类型
		EventType eventType = event.getType();
		// 受影响的path
		String path = event.getPath();
		String logPrefix = "【Watcher-" + this.seq.incrementAndGet() + "】";

		LOG.info( logPrefix + "收到Watcher通知" );
		LOG.info( logPrefix + "连接状态:\t" + keeperState.toString() );
		LOG.info( logPrefix + "事件类型:\t" + eventType.toString() );

		if ( KeeperState.SyncConnected == keeperState ) {
			// 成功连接上ZK服务器
			if ( EventType.None == eventType ) {
				LOG.info( logPrefix + "成功连接上ZK服务器" );
				try {
					byte [] initdata = zk.getData(ZK_PATH,true,null);
					String data = new String(initdata,"UTF-8");
					//LOG.info(LOG_PREFIX_OF_MAIN+" "+data );
					if (!"".equals(data)){
						propertySetValue(data);
					}

				} catch (KeeperException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				connectedSemaphore.countDown();
			} else if ( EventType.NodeCreated == eventType ) {
				LOG.info( logPrefix + "节点创建" );
			} else if ( EventType.NodeDataChanged == eventType ) {
				LOG.info( logPrefix + "节点数据更新" );
				LOG.info(logPrefix+"-----更新后的内同 start -------");
				String context = this.readData(ZK_PATH,true);
				if (!"".equals(context)){
					propertySetValue(context);
				}

				//LOG.info( logPrefix + "\n 数据内容:\n" + this.readData( ZK_PATH, true ) );
				LOG.info(logPrefix+"-----更新后的内同 end -------");
			} else if ( EventType.NodeChildrenChanged == eventType ) {
				LOG.info( logPrefix + "子节点变更" );
			} else if ( EventType.NodeDeleted == eventType ) {
				LOG.info( logPrefix + "节点 " + path + " 被删除" );
			}

		} else if ( KeeperState.Disconnected == keeperState ) {
			LOG.info( logPrefix + "与ZK服务器断开连接" );
		} else if ( KeeperState.AuthFailed == keeperState ) {
			LOG.info( logPrefix + "权限检查失败" );
		} else if ( KeeperState.Expired == keeperState ) {
			LOG.info( logPrefix + "会话失效" );
		}

		LOG.info( "--------------------------------------------end" );

	}

	private void propertySetValue(String context) {
		String[]contextTemp = context.split(";");
		Map<String,String> protertyMap = new HashMap<String,String>();
		for(int i=0;i<contextTemp.length;i++){
            String[]propertiestemp = contextTemp[i].split("@");
            protertyMap.put(StringUtil.trimToEmpty(propertiestemp[0]),StringUtil.trimToEmpty(propertiestemp[1].replaceAll(";","")));
        }
		PropertiesUtil.setPropertyValue(protertyMap,CONFIG_FILE);
	}
}