type LogLevel = "debug" | "info" | "warn" | "error";

class Logger {
  private isDev = import.meta.env.MODE === "development";

  private formatMessage(level: LogLevel, message: string, data?: any) {
    const timestamp = new Date().toISOString();
    return `[${timestamp}] [${level.toUpperCase()}] ${message}${data ? ' \n' + JSON.stringify(data, null, 2) : ''}`;
  }

  debug(message: string, data?: any) {
    if (this.isDev) {
      console.debug(this.formatMessage("debug", message, data));
    }
  }

  info(message: string, data?: any) {
    if (this.isDev) {
      console.info(this.formatMessage("info", message, data));
    }
    // Future: send to telemetry provider (e.g. OpenTelemetry)
  }

  warn(message: string, data?: any) {
    console.warn(this.formatMessage("warn", message, data));
    // Future: send warning to monitoring
  }

  error(message: string, error?: any) {
    console.error(this.formatMessage("error", message, error));
    // Future: send error to Sentry or similar
  }
}

export const logger = new Logger();
