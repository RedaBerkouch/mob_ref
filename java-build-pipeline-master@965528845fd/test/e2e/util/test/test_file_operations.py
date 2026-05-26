import unittest
from unittest.mock import mock_open, patch
from file_operations import replace_in_file

class TestFileOperations(unittest.TestCase):

    @patch("builtins.open", new_callable=mock_open, read_data="old content")
    @patch("file_operations.logger")
    def test_replace_in_file(self, mock_logger, mock_file):
        file_path = "test_file.txt"
        old_text = "old"
        new_text = "new"

        replace_in_file(file_path, old_text, new_text)

        # Check if the file was opened twice (read and write)
        self.assertEqual(mock_file.call_count, 2)

        # Check if the content was replaced correctly
        mock_file().write.assert_called_once_with("new content")

        # Verify logging
        mock_logger.info.assert_called_once_with(f"Replaced content in file successfully: {file_path}")

if __name__ == "__main__":
    unittest.main()