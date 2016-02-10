package com.cts.mule.router;

import org.mule.routing.UntilSuccessfulConfiguration;

public interface DynamicUntilSucessfulConfig extends
		UntilSuccessfulConfiguration {

	String getMaxRetriesExpression();

}
