# ppl_branch_type.py


def get_branch_type_from_branch_name(branch_name: str) -> str:
    """
    Determines the branch type based on the provided branch name in a GIT workflow.

    This function identifies the branch type (e.g., master, develop, feature, hotfix, release)
    based on common GIT naming conventions. If the branch name does not match any known patterns,
    it defaults to 'feature'. If no branch name is provided (None), the function returns 'develop'.

    Parameters:
    -----------
    branch_name : str
        The name of the branch (e.g., 'master', 'develop', 'feature/some-feature', etc.).

    Returns:
    --------
    str
        A string representing the type of the branch, which can be one of the following:
        - 'master' (for 'master' or 'main' branches)
        - 'develop' (for 'develop' branch)
        - 'feature' (for branches starting with 'feature/' or as a fallback)
        - 'hotfix' (for branches starting with 'hotfix/')
        - 'release' (for branches starting with 'release/')
    """


    if branch_name is None:
        # We are outside a GIT workflow, fallback to DEVELOP
        return "develop"

    if branch_name.lower() == "master" or branch_name.lower() == "main":
        return "master"
    elif branch_name.lower() == "develop":
        return "develop"
    elif branch_name.lower().startswith("feature"):
        return "feature"
    elif branch_name.lower().startswith("hotfix"):
        return "hotfix"
    elif branch_name.lower().startswith("release"):
        return "release"
    # Otherwise this is a branch not following the pattern... Fallback to FEATURE
    return "feature"


