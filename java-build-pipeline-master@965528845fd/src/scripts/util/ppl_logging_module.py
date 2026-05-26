# ppl_logging_module.py
import logging


# Log levels:
# WARNING -> general information
# INFO -> debug level information
# DEBUG -> messages that can contain passwords
log = logging.getLogger()


def setup_logger(verbose_log):
    if verbose_log == "true":
        log_level = logging.INFO
    else:
        log_level = logging.WARNING

    log.setLevel(log_level)
    handler = logging.StreamHandler()
    handler.setFormatter(logging.Formatter("%(message)s"))
    log.addHandler(handler)
    log.warning(f"log level: {logging.getLevelName(log_level)}")
    return log


def log_compliant_mode_warning(compliant_mode):
    if compliant_mode:
        log.warning("##################################################################################")
        log.warning("# Pipeline in compliant-mode, disabling of security-checks will not take effect. #")
        log.warning("# see: https://confluence.bit.admin.ch/x/XiNkIg for how to deal with them.       #")
        log.warning("##################################################################################")


def log_new_line():
    log.warning("")


def log_separator_line():
    log.warning("****************************************")
