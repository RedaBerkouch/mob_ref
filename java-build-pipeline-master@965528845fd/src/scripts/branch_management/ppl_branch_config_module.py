# ppl_branch_config_module.py

def get_config_for_prefix_pattern(branch_config: dict,
                                  branch_name: str) -> {}:
    """
    Retrieves the configuration for a given branch name, based on prefix matching.

    This function searches through a provided branch configuration dictionary, where keys
    can be exact branch names or prefix patterns (ending with "*"). It returns the configuration
    for the first prefix or exact branch name that matches the provided branch name.

    Args:
        branch_config (dict): A dictionary where keys are branch names or prefixes and values
            are branch-specific configurations.
        branch_name (str): The name of the branch for which to retrieve the configuration.

    Returns:
        dict: The configuration dictionary for the matched branch or prefix. If no match is found,
            an empty dictionary is returned.
    """

    config_for_branch = {}
    for branch_name_or_prefix in branch_config.keys():
        custom_config = branch_config[branch_name_or_prefix]
        if current_branch_starts_with_prefix_pattern(branch_name_or_prefix=branch_name_or_prefix,
                                                     branch_name=branch_name):
            config_for_branch = custom_config
    return config_for_branch


# @return true if the branch configuration is a prefix, and the current branch starts with the prefix
def current_branch_starts_with_prefix_pattern(branch_name_or_prefix: str,
                                              branch_name: str) -> bool:
    """
    Checks if a branch name matches a prefix pattern from the branch configuration.

    This function determines if a given branch name starts with a specified prefix. The prefix
    is defined by a branch configuration key ending with "*", indicating that any branch name
    starting with that prefix should match.

    Args:
       branch_name_or_prefix (str): The branch name or prefix pattern to check against.
           A prefix pattern ends with "*".
       branch_name (str): The branch name to check for a matching prefix.

    Returns:
       bool: True if `branch_name` starts with the specified prefix pattern, False otherwise.
    """

    is_prefix = branch_name_or_prefix.endswith("*")
    if is_prefix:
        prefix = branch_name_or_prefix[:len(branch_name_or_prefix) - 1]
        return branch_name.startswith(prefix)
    else:
        return False
