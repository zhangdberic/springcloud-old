package sc.com.hystrix.concurrentstrategy;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import com.netflix.hystrix.strategy.HystrixPlugins;

/**
 * hystrix配置类
 * @author zhangdb
 *
 */
@Configuration
@ConditionalOnProperty(value = "hystrix.concurrent_strategy.enabled", havingValue = "true")
public class HystrixConfiguration {

	@Autowired(required = false)
	private Collection<HystrixCallableWrapper> wrappers = new ArrayList<>();

	@PostConstruct
	public void init() {
		if(!CollectionUtils.isEmpty(this.wrappers)) {
			HystrixPlugins.getInstance().registerConcurrencyStrategy(new HystrixConcurrencyStrategyCustom(this.wrappers));
		}
	}

}
