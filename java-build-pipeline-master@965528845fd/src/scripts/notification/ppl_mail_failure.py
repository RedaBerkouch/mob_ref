# ppl_mail_failure.py
import os
import sys
import smtplib
import subprocess
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

# Specify the relative paths of the modules to be imported in order to develop locally with them.
# For more, take a lock at the README.md.
module_paths = os.getenv('MODULE_PATHS', ['../util'])
for path in module_paths:
    sys.path.append(os.path.abspath(path))

# ----- Modul imports -----
import ppl_logging_module
from ppl_logging_module import log
from ppl_notification import get_failed_tasks, get_notification_texts, get_cluster_console_url

# ----- Modul parameter -----
compliant_mode = True

def main(arguments):
    # first argument is read in __main__
    git_pusher_email = arguments[2].strip()
    extra_recipients = arguments[3].strip()
    sender = arguments[4]
    server_host = arguments[5]
    server_port = arguments[6]
    pipeline_run_namespace = arguments[7]
    pipeline_run_name = arguments[8]

    log.info(f"sender: {sender}")
    log.info(f"server_host: {server_host}")
    log.info(f"server_port: {server_port}")
    log.info(f"pipeline_run_namespace: {pipeline_run_namespace}")
    log.info(f"pipeline_run_name: {pipeline_run_name}")

    if not git_pusher_email and not extra_recipients:
        log.warning("Both git_pusher_recipient and recipients are empty, skip notification")
        sys.exit()

    extra_list = [r.strip() for r in extra_recipients.split(",") if r.strip()]
    recipients_list = [git_pusher_email.strip()] if git_pusher_email else []
    recipients_list.extend(extra_list)

    recipients_list = list(set(recipients_list))

    recipients_string = ",".join(recipients_list)
    log.info(f"recipients_string: {recipients_string}")

    failed_tasks = get_failed_tasks(pipeline_run_name=pipeline_run_name)
    cluster_console_url = get_cluster_console_url()

    subjectText, text = get_notification_texts(failed_tasks=failed_tasks, cluster_console_url=cluster_console_url,
                                               pipeline_run_namespace=pipeline_run_namespace, pipeline_run_name=pipeline_run_name)

    send_mail(subjectText=subjectText, text=text,
              recipients_string=recipients_string, sender=sender,
              server_host=server_host, server_port=server_port,
              pipeline_run_namespace=pipeline_run_namespace, pipeline_run_name=pipeline_run_name)

def send_mail(subjectText: str, text: str,
              recipients_string: str, sender: str,
              server_host: str, server_port: str,
              pipeline_run_namespace: str, pipeline_run_name: str):
    if subjectText is None:
        log.info(f"No subject text, skip notification")
        sys.exit()

    recipients =  [item.strip() for item in recipients_string.split(",")]

    actor_email = (subprocess.check_output(f"""oc get pr {pipeline_run_name} -o json \
                    | jq -r '.metadata.annotations."pipeline.bit.admin.ch/actor-email"'
                """, shell=True)).decode("utf-8")
    if actor_email and not actor_email.startswith('null'):
        actor_email = actor_email.rstrip()
        log.info(f"Add {actor_email} as recipient")
        recipients.append(actor_email)

    if len(recipients) == 0:
        log.warning("RECIPIENTS is empty, skip notification")
        sys.exit()

    if sender == "":
        log.warning("SENDER is empty, skip notification")
        sys.exit()

    comma_space = ', '
    msg = MIMEMultipart()
    msg['From'] = sender
    msg['To'] = comma_space.join(recipients)
    msg['Subject'] = f"[Tekton PipelineRun {subjectText}] {pipeline_run_namespace}/{pipeline_run_name}"
    log.info(f"Send notification from: {msg['From']}, to: {msg['To']}, subject: {msg['Subject']}")
    msg.attach(MIMEText(text))
    smtp = smtplib.SMTP(server_host, server_port)
    smtp.sendmail(msg['From'], recipients, msg.as_string())
    smtp.quit()

if __name__ == "__main__":
    args = sys.argv
    verbose_log = args[1]
    ppl_logging_module.setup_logger(verbose_log)
    ppl_logging_module.log_compliant_mode_warning(compliant_mode)
    main(args)
