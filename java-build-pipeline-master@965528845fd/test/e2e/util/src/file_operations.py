# file_operations.py
from logger import get_logger

# Module-level logger
logger = get_logger()


def override_file(file_path, content):
    """
    Overrides the entire content of a file.

    :param file_path: Path to the file.
    :param content: The new content to write to the file.
    """
    try:
        with open(file_path, 'w') as file:
            file.write(content)
        logger.debug(f"File overridden successfully: {file_path}")
    except Exception as e:
        logger.error(f"Failed to override file: {e}")
        raise


def replace_in_file(file_path, old_text, new_text):
    """
    Replaces specific content in a file.

    :param file_path: Path to the file.
    :param old_text: The text to be replaced.
    :param new_text: The text to replace with.
    """
    try:
        with open(file_path, 'r') as file:
            content = file.read()

        updated_content = content.replace(old_text, new_text)

        with open(file_path, 'w') as file:
            file.write(updated_content)

        logger.info(f"Replaced content in file successfully: {file_path}")
    except Exception as e:
        logger.error(f"Failed to replace content in file: {e}")
        raise