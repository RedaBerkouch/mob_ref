# ppl_namespace_stage_module.py


# Function to determine the next stage
def get_next_stage(automated_staging, actual_stage):
    for stage in automated_staging:
        if ":" not in stage:
            print(f"Ungültiger Eintrag: {stage}")
            continue
        current, next_stage = stage.split(":")
        if current == actual_stage:
            return next_stage
    return ""


# Supported stages: d:dev, r:ref, a:abn, p:prod, t:training, s:subjecttest
# For all other stages, stage_short and stage_long will be the same.
def get_stage(stage):
    stage_short = stage
    stage_long = stage

    if stage == "d" or stage == "dev":
        stage_short = "d"
        stage_long = "dev"
    elif stage == "r" or stage == "ref":
        stage_short = "r"
        stage_long = "ref"
    elif stage == "a" or stage == "abn":
        stage_short = "a"
        stage_long = "abn"
    elif stage == "p" or stage == "prod":
        stage_short = "p"
        stage_long = "prod"
    elif stage == "t" or stage == "training":
        stage_short = "t"
        stage_long = "training"
    elif stage == "s" or stage == "subjecttest":
        stage_short = "s"
        stage_long = "subjecttest"
    elif stage == "prd":
        stage_short = "p"
        stage_long = "prd"

    return stage_short, stage_long


def determine_target_namespace(namespace_prefix: str,
                               ppl_namespace_stage: str,
                               branch_deploy_stage: str):
    # for backwards compatibility, we use the value that is specified on a pipeline level,
    # if you want to use the branch-level stage, remove the config from pipeline-level.
    if ppl_namespace_stage:
        effective_stage = ppl_namespace_stage[:1]
    else:
        effective_stage = branch_deploy_stage[:1]

    return f"{namespace_prefix}-{effective_stage}"
