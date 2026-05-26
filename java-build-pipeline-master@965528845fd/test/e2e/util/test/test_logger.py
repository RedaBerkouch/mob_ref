import logging
from unittest.mock import patch, MagicMock

from logger import setup_logger, get_logger, get_test_formatter


def test_setup_logger_default_formatter():
    with patch("sys.stdout", new=MagicMock()):
        logger = setup_logger()

        assert logger.level == logging.INFO

        assert len(logger.handlers) == 1

        handler = logger.handlers[0]
        assert isinstance(handler, logging.StreamHandler)

        assert handler.formatter._fmt == "%(levelname)s: %(message)s"

        assert logger.propagate is False


def test_setup_logger_custom_formatter():
    custom_formatter = logging.Formatter("CUSTOM - %(message)s")

    with patch("sys.stdout", new=MagicMock()):
        logger = setup_logger(formatter=custom_formatter, logger_name="mylogger")

        handler = logger.handlers[0]
        assert handler.formatter is custom_formatter
        assert logger.name == "mylogger"


def test_get_logger_default():
    fake_frame = MagicMock()
    fake_frame.f_globals = {"__name__": "my_module"}

    with patch("sys._getframe", return_value=fake_frame):
        logger = get_logger()

        assert logger.name == "my_module"
        assert len(logger.handlers) == 1
        assert logger.handlers[0].formatter._fmt == "%(levelname)s: %(message)s"


def test_get_logger_with_test_name():
    fake_frame = MagicMock()
    fake_frame.f_globals = {"__name__": "my_module"}

    with patch("sys._getframe", return_value=fake_frame):
        logger = get_logger("test_case_1")

        assert logger.name == "my_module.test_case_1"

        handler = logger.handlers[0]
        assert handler.formatter._fmt == "test_case_1 - %(levelname)s - %(message)s"


def test_get_logger_clears_existing_handlers():
    fake_frame = MagicMock()
    fake_frame.f_globals = {"__name__": "module_x"}

    logger = logging.getLogger("module_x")
    logger.addHandler(logging.StreamHandler())

    with patch("sys._getframe", return_value=fake_frame):
        new_logger = get_logger()

        assert len(new_logger.handlers) == 1


def test_get_test_formatter():
    formatter = get_test_formatter("case42")
    assert formatter._fmt == "case42 - %(levelname)s - %(message)s"
