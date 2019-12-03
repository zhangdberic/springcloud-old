package sc.com.hystrix.concurrentstrategy;

import java.util.concurrent.Callable;
/**
 * hystrix Callback包装器
 * 使用装饰器模式,对参数callable进行包装.
 * @author zhangdb
 *
 */
public interface HystrixCallableWrapper {
	
	<T> Callable<T> wrap(Callable<T> callable);

}
