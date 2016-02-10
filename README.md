# DynamicUntillSuccessfulRouter
This Router extends existing until-successful scope from Mule to add Dynamic Mule Expression on Max Retries value.

The documentation for the existing untill-successfull scope
https://docs.mulesoft.com/mule-user-guide/v/3.6/until-successful-scope

The new router config will now look like following

<custom-router class="com.cts.mule.router.DynamicUntilSucessfulRouter">
	<http:outbound-endpoint address="http://xyz.yz" exchange-pattern="request-response" method="POST" doc:name="HTTP" responseTimeout="5000"/>
	<spring:property name="maxRetriesExpression" value="#[2 + 2]" />
	<spring:property name="maxRetries" value="2" />
	<spring:property name="failureExpression" value="#[exception != null &amp;&amp; (exception.causedBy(java.net.ConnectException) || exception.causedBy(java.net.SocketTimeoutException)) || message.inboundProperties['http.status'] != 200]" />
	<spring:property name="synchronous" value="true" />
	<spring:property name="secondsBetweenRetries" value="5" />
</custom-router>
