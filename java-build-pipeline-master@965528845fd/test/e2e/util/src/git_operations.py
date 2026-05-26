from git import Repo, GitCommandError
from logger import get_logger

# Initialize logger for git_operations
logger = get_logger()

def clone_repository(repo_url: str, repo_dir: str) -> None:
    """
    Clone a Git repository to the specified directory.

    Args:
        repo_url (str): The URL of the Git repository to clone.
        repo_dir (str): The directory where the repository will be cloned.

    Returns:
        None

    Raises:
        GitCommandError: If the cloning process fails.
    """
    try:
        logger.debug(f"Cloning repository: {repo_url} into {repo_dir}")
        Repo.clone_from(repo_url, repo_dir)
        logger.debug(f"Repository cloned into {repo_dir}")
    except Exception as e:
        logger.error(f"Failed to clone repository: {e}")
        raise


def branch_exists(repo_dir: str, branch_name: str) -> bool:
    """
    Check if a branch exists in the Git repository.

    Args:
        repo_dir (str): The directory of the Git repository.
        branch_name (str): The name of the branch to check.

    Returns:
        bool: True if the branch exists, False otherwise.
    """
    try:
        repo = Repo(repo_dir)
        exists = branch_name in repo.heads
        logger.debug(f"Branch '{branch_name}' exists: {exists}")
        return exists
    except Exception as e:
        logger.error(f"Failed to check if branch '{branch_name}' exists: {e}")
        raise


def create_and_checkout_branch(repo_dir: str, branch_name: str) -> None:
    """
    Create and checkout a new branch in the Git repository.

    Args:
        repo_dir (str): The directory of the Git repository.
        branch_name (str): The name of the branch to create and checkout.

    Returns:
        None
    """
    try:
        repo = Repo(repo_dir)
        new_branch = repo.create_head(branch_name)
        new_branch.checkout()
        logger.debug(f"Created and checked out branch: {branch_name}")
    except Exception as e:
        logger.error(f"Failed to create or checkout branch '{branch_name}': {e}")
        raise


def get_current_commit_id(repo_dir: str) -> str:
    """
    Get the current commit ID of the Git repository.

    Args:
        repo_dir (str): The directory of the Git repository.

    Returns:
        str: The current commit ID.
    """
    try:
        repo = Repo(repo_dir)
        commit_id = repo.head.commit.hexsha
        logger.debug(f"Current commit ID: {commit_id}")
        return commit_id
    except Exception as e:
        logger.error(f"Failed to retrieve current commit ID: {e}")
        raise


def commit_and_push_changes(repo_dir: str, commit_message: str) -> str:
    """
    Commit and push changes to the remote repository.

    Args:
        repo_dir (str): The directory of the Git repository.
        commit_message (str): The commit message to use.

    Returns:
        str: The commit SHA of the new commit.

    Raises:
        GitCommandError: If the push operation fails.
    """
    try:
        repo = Repo(repo_dir)
        repo.git.add(A=True)  # Stage all changes
        commit = repo.index.commit(commit_message)  # Commit changes
        origin = repo.remote(name='origin')
        current_branch = repo.active_branch

        # Push changes
        origin.push(refspec=f"{current_branch.name}:{current_branch.name}")
        logger.debug("Changes committed and pushed to the current branch.")

        return commit.hexsha  # Return the commit SHA
    except Exception as e:
        logger.error(f"Failed to commit or push changes: {e}")
        raise


def delete_branch(repo_dir: str, branch_name: str, force: bool = True, branch_to_switch: str = "develop") -> None:
    """
    Delete a branch from the Git repository.

    Args:
        repo_dir (str): The directory of the Git repository.
        branch_name (str): The name of the branch to delete.
        force (bool): Whether to force delete the branch. Defaults to True.
        branch_to_switch (str): The branch to switch to before deleting the branch. Defaults to "develop".

    Returns:
        None
    """
    repo = Repo(repo_dir)

    # Delete locally
    try:
        if repo.active_branch.name == branch_name:
            repo.git.checkout(branch_to_switch)
            logger.debug(f"Switched to '{branch_to_switch}' branch to allow deletion of '{branch_name}'.")
        branch = repo.heads[branch_name]
        branch.delete(repo, branch_name, force=force)
        logger.debug(f"Branch '{branch_name}' deleted locally.")
    except IndexError:
        logger.warning(f"Branch '{branch_name}' does not exist locally. Skipping local deletion.")
    except GitCommandError as e:
        logger.error(f"Failed to delete local branch '{branch_name}': {e}")

    # Delete remotely (always attempt, regardless of local result)
    try:
        origin = repo.remote(name='origin')
        origin.push(refspec=f":{branch_name}")
        logger.debug(f"Branch '{branch_name}' deleted from remote.")
    except GitCommandError as e:
        logger.error(f"Failed to delete remote branch '{branch_name}': {e}")
