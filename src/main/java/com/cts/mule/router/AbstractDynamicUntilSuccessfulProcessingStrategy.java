package com.cts.mule.router;

import java.io.NotSerializableException;
import java.io.Serializable;

import org.mule.DefaultMuleMessage;
import org.mule.VoidMuleEvent;
import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.MuleRuntimeException;
import org.mule.config.i18n.MessageFactory;
import org.mule.routing.AbstractUntilSuccessfulProcessingStrategy;
import org.mule.routing.UntilSuccessfulConfiguration;

public abstract class AbstractDynamicUntilSuccessfulProcessingStrategy extends
		AbstractUntilSuccessfulProcessingStrategy {
	private DynamicUntilSucessfulConfig untilSuccessfulConfiguration;

	@Override
	public void setUntilSuccessfulConfiguration(
			final UntilSuccessfulConfiguration untilSuccessfulConfiguration) {
		this.untilSuccessfulConfiguration = (DynamicUntilSucessfulConfig) untilSuccessfulConfiguration;
	}

	/**
	 * Process the event through the configured route in the until-successful
	 * configuration.
	 *
	 * @param event
	 *            the event to process through the until successful inner route.
	 * @return the response from the route if there's no ack expression. If
	 *         there's ack expression then a message with the response event but
	 *         with a payload defined by the ack expression.
	 */
	protected MuleEvent processEvent(final MuleEvent event) {
		MuleEvent returnEvent;
		try {
			returnEvent = untilSuccessfulConfiguration.getRoute()
					.process(event);
		} catch (final MuleException me) {
			throw new MuleRuntimeException(me);
		}

		if (returnEvent == null
				|| VoidMuleEvent.getInstance().equals(returnEvent)) {
			return returnEvent;
		}

		final MuleMessage msg = returnEvent.getMessage();
		if (msg == null) {
			throw new MuleRuntimeException(
					MessageFactory
							.createStaticMessage("No message found in response to processing, which is therefore considered failed for event: "
									+ event));
		}

		final boolean errorDetected = untilSuccessfulConfiguration
				.getFailureExpressionFilter().accept(msg);
		if (errorDetected) {
			throw new MuleRuntimeException(
					MessageFactory
							.createStaticMessage("Failure expression positive when processing event: "
									+ event));
		}
		return returnEvent;
	}

	/**
	 * @param event
	 *            the response event from the until-successful route.
	 * @return the response message to be sent to the until successful caller.
	 */
	protected MuleEvent processResponseThroughAckResponseExpression(
			MuleEvent event) {
		if (event == null || VoidMuleEvent.getInstance().equals(event)) {
			return VoidMuleEvent.getInstance();
		}
		if (untilSuccessfulConfiguration.getAckExpression() == null) {
			return event;
		}
		event.getMessage().setPayload(
				getUntilSuccessfulConfiguration()
						.getMuleContext()
						.getExpressionManager()
						.evaluate(
								getUntilSuccessfulConfiguration()
										.getAckExpression(), event));
		return event;
	}

	/**
	 * @return configuration of the until-successful router.
	 */
	protected DynamicUntilSucessfulConfig getUntilSuccessfulConfiguration() {
		return untilSuccessfulConfiguration;
	}

	@Override
	public MuleEvent route(MuleEvent event) throws MessagingException {
		prepareAndValidateEvent(event);
		return doRoute(event);
	}

	protected abstract MuleEvent doRoute(final MuleEvent event)
			throws MessagingException;

	private void prepareAndValidateEvent(final MuleEvent event)
			throws MessagingException {
		try {
			final MuleMessage message = event.getMessage();
			if (message instanceof DefaultMuleMessage) {
				if (((DefaultMuleMessage) message).isConsumable()) {
					message.getPayloadAsBytes();
				} else {
					ensureSerializable(message);
				}
			} else {
				message.getPayloadAsBytes();
			}
		} catch (final Exception e) {
			throw new MessagingException(
					MessageFactory
							.createStaticMessage("Failed to prepare message for processing"),
					event, e, getUntilSuccessfulConfiguration().getRouter());
		}
	}

	protected void ensureSerializable(MuleMessage message)
			throws NotSerializableException {
		if (!(message.getPayload() instanceof Serializable)) {
			throw new NotSerializableException(message.getPayload().getClass()
					.getCanonicalName());
		}
	}

}
