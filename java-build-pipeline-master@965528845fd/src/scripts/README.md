# Coding with Python

## Modularization

Due to the continuous growth of the pipeline and the addition of new features and tasks, as well as the reuse of individual code sections across multiple tasks in different pipelines, the Python scripts have been modularized.
Essentially, the various modules are structured into functional folders. To maintain a consistent scripting language, all tasks should be written in Python whenever possible. Additionally, efforts should be made to implement fewer steps in the tasks to save on performance.

### Structure of the Modules

- The structuring should occur on two levels:
    - Larger groupings by functional areas should be done at the folder level.
    - A module then encapsulates a sub-area and implements all relevant functions.
- Each module starts with the prefix `ppl_`, making them easily identifiable as our pipeline script modules.
- The suffix of each module is `_module`.
- Each pipeline has a setup task that prepares the pipeline and its parameters. The setup task ultimately calls a start script in the `ppl_setup` folder. There is a `1:1` dependency.
- Each file needs a unique name within the built Helm files due to dynamic import requirements.

### Dynamic Module Imports

It is noticeable that a classic Python package and module structure is not used. This is due to a trade-off between local developability and execution by Tekton.
Essentially, all Python files are defined in a ConfigMap and stored on a common hierarchy level on a volume.
Thus, the path to the modules is always in the same directory `./`. During local development, however, not all files should be in one directory.
We want to group our scripts and package them into modules for better clarity and abstraction.

The import path between local development and execution in the Tekton pipeline differs and must be set dynamically.
The environment variable `MODULE_PATHS` is used to toggle this.
To enable switching, the path should be provided to the operating system as standard during local development.
On the pipeline level, this is set to `.`, as all modules are in the same directory. Consequently, no `__init__.py` can be used, as otherwise, multiple files with the same name would exist on one level.

The following code snippet enables this:

```python
# ppl_build_setup.py
import sys
import os

# Specify the relative paths of the modules to be imported in order to develop locally with them.
# For more, take a look at the README.md.
module_paths = os.getenv('MODULE_PATHS', ['../util'])
for path in module_paths:
    sys.path.append(os.path.abspath(path))

# ----- Module imports -----
import ppl_logging_module
```
### Logging

A global logger should be used by all modules. For this purpose, a logging module is provided, which imports the logger as follows: from ppl_logging_module import log.

```python
from ppl_logging_module import log

log.info("Hello pipeline.")
```

This can optionally be configured at the beginning of a script according to the log levels.

```python
verbose_log = args[1]
ppl_logging_module.setup_logger(verbose_log)
```

The following log levels are available and should be used:
* `WARNING` -> general information
* `INFO` -> debug level information
* `DEBUG` -> messages that can contain passwords

### File Handling
When reading and writing files, the same problem occurs.
These files may be in a completely different folder during local development than they are specified in the ConfigMap.
As shown in this example, the problem can be solved by querying the environment variable again and adjusting the path accordingly:

```python
module_paths = os.getenv('MODULE_PATHS', ['../util', '../credentials'])

if module_paths == '.':
    input_file = open('/scripts/template-npmrc', 'r')
else:
    input_file = open('./template-npmrc', 'r')

target_file = Path(f'{ws_workspace_path}/{run_directory}/source/etc/npmrc')
```
### Working with IntelliJ
To work properly with Python in IntelliJ despite the workaround, the individual folders at the first level should be marked via right mouse click as  `Mark Directory as` -> `Sources Root`.

