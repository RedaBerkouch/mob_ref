# ppl_common_module.py
from typing import Union
from ppl_logging_module import log


def write_result(content: Union[str, bool],
                 path: str):
    if isinstance(content, bool):
        str_content = ("false", "true")[content]
    else:
        str_content = content

    log.warning(f"{path}: {str_content}")
    open(path, "w").write(str_content)
