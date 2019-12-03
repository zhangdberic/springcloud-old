package sc.com.hystrix.concurrentstrategy;

import java.util.Collection;
import java.util.concurrent.Callable;

import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
/**
 * HystrixConcurrencyStrategy实现
 * 对构造方法参数wrappers逐个执行包装,包装后最终的Callable返回给hystrix使用.
 * @author zhangdb
 *
 */
public class HystrixConcurrencyStrategyCustom extends HystrixConcurrencyStrategy {
	/** 装饰器列表 */
	private final Collection<HystrixCallableWrapper> wrappers;

	public HystrixConcurrencyStrategyCustom(Collection<HystrixCallableWrapper> wrappers) {
		this.wrappers = wrappers;
	}

	@Override
	public <T> Callable<T> wrapCallable(Callable<T> callable) {
		Callable<T> delegate = callable;
		for (HystrixCallableWrapper wrapper : this.wrappers) {
			delegate = wrapper.wrap(delegate);
		}
		return delegate;
	}

}
