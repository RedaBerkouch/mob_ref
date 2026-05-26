import sys
from typing import Optional
import logging


def setup_logger(formatter: logging.Formatter = None, logger_name: str = None) -> logging.Logger:
    """
    Set up a logger with consistent configuration for GitHub Actions.
    Always uses the calling module's name and INFO level.

    Args:
        formatter (logging.Formatter, optional): Custom formatter for the logger. Defaults to None.
        logger_name (str, optional): Unique name for the logger. Defaults to None.

    Returns:
        Configured logger instance
    """
    # Use the provided logger name or the calling module's name
    frame = sys._getframe(1)
    name = logger_name or frame.f_globals.get('__name__', 'tekton')

    logger = logging.getLogger(name)

    # Clear existing handlers to avoid duplicates
    logger.handlers.clear()

    logger.setLevel(logging.INFO)

    # Create a new console handler
    handler = logging.StreamHandler(sys.stdout)
    handler.setLevel(logging.INFO)

    # Use the provided formatter or default formatter
    if not formatter:
        formatter = logging.Formatter('%(levelname)s: %(message)s')
    handler.setFormatter(formatter)

    logger.addHandler(handler)

    # Prevent propagation to root logger to avoid duplicate logs
    logger.propagate = False

    return logger


def get_logger(test_name: Optional[str] = None) -> logging.Logger:
    """
    Get a logger instance. If not already configured, sets it up with default configuration.
    Always uses the calling module's name and INFO level.

    Args:
        test_name (Optional[str]): The name of the test case to customize the formatter. Defaults to None.

    Returns:
        Logger instance
    """
    # Get the name of the calling module
    frame = sys._getframe(1)
    base_name = frame.f_globals.get('__name__', 'tekton')

    # Include test_name in the logger name to make it unique
    name = f"{base_name}.{test_name}" if test_name else base_name

    logger = logging.getLogger(name)

    # Clear existing handlers to ensure isolation
    logger.handlers.clear()

    # Set up the logger with a specific formatter
    if test_name:
        formatter = get_test_formatter(test_name)
    else:
        formatter = logging.Formatter('%(levelname)s: %(message)s')

    return setup_logger(formatter, logger_name=name)


def get_test_formatter(test_name: str) -> logging.Formatter:
    """
    Generate a formatter specific to a test case.

    Args:
        test_name (str): The name of the test case (e.g., 'test_1', 'test_2').

    Returns:
        logging.Formatter: A formatter customized for the test case.
    """
    return logging.Formatter(f'{test_name} - %(levelname)s - %(message)s')
