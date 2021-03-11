package gov.nyc.nyco.intellimatch.filters;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Filter login and logout into separate log file.
 * 
 */
public class LoginLoggerFilter extends Filter<ILoggingEvent>  {
	  @Override
	  public FilterReply decide(ILoggingEvent event) {    
	    if (event.getMessage().toLowerCase().contains("login") || event.getMessage().toLowerCase().contains("logged out")) {
	      return FilterReply.ACCEPT;
	    } else {
	      return FilterReply.DENY;
	    }
	  }
}
