"""Config utility"""

import logging
import sys


def configure_logging(args: str,
                      log: logging.Logger):
    """Configure Logger object."""
    # logging.basicConfig(level=logging.INFO)
    log_level = getattr(logging, args.log_level.upper(), logging.INFO)
    logging.basicConfig(level=log_level,
                        format='%(asctime)s %(name)-12s %(levelname)-8s %(message)s',
                        datefmt='%Y-%m-%d %H:%M:%S')
    log.debug(f"Log level: {logging.getLevelName(log_level)}")
    log.debug(f"Running python version: {sys.version}")