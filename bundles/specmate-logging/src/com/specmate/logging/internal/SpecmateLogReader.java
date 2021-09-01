package com.specmate.logging.internal;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

import com.specmate.logging.internal.config.SpecmateLogReaderConfig;

@Component(service = LogListener.class, immediate = true, configurationPid = SpecmateLogReaderConfig.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class SpecmateLogReader implements LogListener {

	/** The log level threshold */
	private LogLevel logLevel;

	private static Map<LogLevel, String> level2String = new HashMap<>();

	static {
		level2String.put(LogLevel.DEBUG, "DEBUG");
		level2String.put(LogLevel.INFO, "INFO");
		level2String.put(LogLevel.WARN, "WARNING");
		level2String.put(LogLevel.ERROR, "ERROR");
	}

	private static Map<String, LogLevel> string2level = new HashMap<>();

	static {
		string2level.put("debug", LogLevel.DEBUG);
		string2level.put("info", LogLevel.INFO);
		string2level.put("warning", LogLevel.WARN);
		string2level.put("error", LogLevel.ERROR);
	}

	/** The log reader service */
	private LogReaderService logReaderService;

	@Activate
	public void activate(Map<String, Object> properties) {
		// if no property is set, use info level
		String confLogLevel = (String) properties.getOrDefault(SpecmateLogReaderConfig.KEY_LOG_LEVEL, "info");

		LogLevel mappedLevel = getLevelFromString(confLogLevel);

		// the mapped level can be null in case the property is not a valid
		// value
		if (mappedLevel != null) {
			logLevel = mappedLevel;
		} else {
			System.out.println("Unknown log level " + confLogLevel);
			logLevel = LogLevel.INFO;
		}
		System.out.println("Setting log level to " + level2String.get(logLevel));
		Enumeration<?> log = logReaderService.getLog();
		while (log.hasMoreElements()) {
			logged((LogEntry) log.nextElement());
		}
		logReaderService.addLogListener(this);
	}

	private String getStringFromLevel(LogLevel level) {
		return level2String.get(level);
	}

	private LogLevel getLevelFromString(String level) {
		return string2level.get(level.toLowerCase());
	}

	@Deactivate
	public void deactivate() {
		logReaderService.removeLogListener(this);
	}

	@Reference
	public void setLogReader(LogReaderService logReaderService) {
		this.logReaderService = logReaderService;
	}

	public void unsetLogReader(LogReaderService logReaderService) {
		logReaderService.removeLogListener(this);
	}

	@Override
	public void logged(LogEntry entry) {
		LinkedList<LogLevel> levelToShow = new LinkedList<LogLevel>();
		switch (logLevel) {
		case ERROR:
			levelToShow.add(LogLevel.ERROR);
			break;
		case WARN:
			levelToShow.add(LogLevel.ERROR);
			levelToShow.add(LogLevel.WARN);
			break;
		case INFO:
			levelToShow.add(LogLevel.ERROR);
			levelToShow.add(LogLevel.WARN);
			levelToShow.add(LogLevel.INFO);
			break;
		case DEBUG:
			levelToShow.add(LogLevel.ERROR);
			levelToShow.add(LogLevel.WARN);
			levelToShow.add(LogLevel.INFO);
			levelToShow.add(LogLevel.DEBUG);
			break;
		default:
			levelToShow.add(LogLevel.ERROR);
			levelToShow.add(LogLevel.WARN);
			levelToShow.add(LogLevel.INFO);
			levelToShow.add(LogLevel.DEBUG);
			break;
		}

		String message = getStringFromLevel(entry.getLogLevel()) + ":" + entry.getBundle().getSymbolicName() + ":"
				+ entry.getMessage();
		if (levelToShow.contains(entry.getLogLevel())) {
			if (entry.getLogLevel() == LogLevel.ERROR || entry.getLogLevel() == LogLevel.WARN) {
				System.err.println(message);
				if (entry.getException() != null) {
					entry.getException().printStackTrace();
				}
			} else {
				System.out.println(message);
			}
		}
	}
}
