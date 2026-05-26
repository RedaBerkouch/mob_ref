from unittest.mock import patch, MagicMock

import pytest
from git import GitCommandError

from git_operations import (
    clone_repository,
    branch_exists,
    create_and_checkout_branch,
    get_current_commit_id,
    commit_and_push_changes,
    delete_branch
)


def test_clone_repository_success():
    with patch("git_operations.Repo.clone_from") as mock_clone:
        clone_repository("https://example.com/repo.git", "/tmp/repo")
        mock_clone.assert_called_once_with("https://example.com/repo.git", "/tmp/repo")


def test_clone_repository_failure():
    with patch("git_operations.Repo.clone_from", side_effect=Exception("clone failed")):
        with pytest.raises(Exception):
            clone_repository("url", "dir")


def test_branch_exists_true():
    mock_repo = MagicMock()
    mock_repo.heads = ["main", "develop"]

    with patch("git_operations.Repo", return_value=mock_repo):
        assert branch_exists("/tmp/repo", "main") is True


def test_branch_exists_false():
    mock_repo = MagicMock()
    mock_repo.heads = ["main"]

    with patch("git_operations.Repo", return_value=mock_repo):
        assert branch_exists("/tmp/repo", "feature") is False


def test_branch_exists_error():
    with patch("git_operations.Repo", side_effect=Exception("repo error")):
        with pytest.raises(Exception):
            branch_exists("/tmp/repo", "main")


def test_create_and_checkout_branch_success():
    mock_branch = MagicMock()
    mock_repo = MagicMock()
    mock_repo.create_head.return_value = mock_branch

    with patch("git_operations.Repo", return_value=mock_repo):
        create_and_checkout_branch("/tmp/repo", "feature")

        mock_repo.create_head.assert_called_once_with("feature")
        mock_branch.checkout.assert_called_once()


def test_create_and_checkout_branch_failure():
    with patch("git_operations.Repo", side_effect=Exception("fail")):
        with pytest.raises(Exception):
            create_and_checkout_branch("/tmp/repo", "feature")


def test_get_current_commit_id_success():
    mock_repo = MagicMock()
    mock_repo.head.commit.hexsha = "abc123"

    with patch("git_operations.Repo", return_value=mock_repo):
        commit_id = get_current_commit_id("/tmp/repo")
        assert commit_id == "abc123"


def test_get_current_commit_id_failure():
    with patch("git_operations.Repo", side_effect=Exception("fail")):
        with pytest.raises(Exception):
            get_current_commit_id("/tmp/repo")


def test_commit_and_push_changes_success():
    mock_repo = MagicMock()
    mock_origin = MagicMock()
    mock_repo.remote.return_value = mock_origin
    mock_repo.active_branch.name = "main"

    with patch("git_operations.Repo", return_value=mock_repo):
        commit_and_push_changes("/tmp/repo", "message")

        mock_repo.git.add.assert_called_once_with(A=True)
        mock_repo.index.commit.assert_called_once_with("message")
        mock_origin.push.assert_called_once_with(refspec="main:main")


def test_commit_and_push_changes_failure():
    with patch("git_operations.Repo", side_effect=Exception("push fail")):
        with pytest.raises(Exception):
            commit_and_push_changes("/tmp/repo", "msg")


def test_delete_branch_success():
    mock_repo = MagicMock()
    mock_repo.active_branch.name = "develop"

    mock_branch = MagicMock()
    mock_repo.heads.__getitem__.return_value = mock_branch

    mock_origin = MagicMock()
    mock_repo.remote.return_value = mock_origin

    with patch("git_operations.Repo", return_value=mock_repo):
        delete_branch("/tmp/repo", "feature")

        mock_branch.delete.assert_called_once()
        mock_origin.push.assert_called_once_with(refspec=":feature")


def test_delete_branch_nonexistent():
    mock_repo = MagicMock()
    mock_repo.heads.__getitem__.side_effect = IndexError()

    with patch("git_operations.Repo", return_value=mock_repo):
        # Should NOT raise
        delete_branch("/tmp/repo", "missing")


def test_delete_branch_remote_error():
    mock_repo = MagicMock()
    mock_repo.active_branch.name = "develop"

    mock_branch = MagicMock()
    mock_repo.heads.__getitem__.return_value = mock_branch

    mock_origin = MagicMock()
    mock_origin.push.side_effect = GitCommandError("push", 1)

    mock_repo.remote.return_value = mock_origin

    with patch("git_operations.Repo", return_value=mock_repo):
        # Should NOT raise GitCommandError (function catches it)
        delete_branch("/tmp/repo", "feature")
