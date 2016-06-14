package monitoring.internal;

import org.apache.commons.lang.StringUtils;

/**
 * Convenience enum to unify application names. Warning: Intended to be used
 * only for metrics.
 * 
 * @author milan.panic
 *
 */
public enum ApplicationNameEnum {
	/** Values should not be changed without changing web.xml */
	SAAS_API("SaaS-API"), EIT("EIT"), PSA("PSA"), AUTH("Auth"), WEB_API("Web-API"), REPORTS("Reports"), TASK("Task"), ;
	
	private String value;
	
	private ApplicationNameEnum(final String value) {
		this.value = value;
	}
	
	public static ApplicationNameEnum fromValue(String value) {
		for (ApplicationNameEnum current : ApplicationNameEnum.values()) {
			if (StringUtils.equals(current.getValue(), value)) {
				return current;
			}
		}
		throw new IllegalArgumentException("Invalid enumeration value=" + value);
	}
	
	public String getValue() {
		return value;
	}
}
