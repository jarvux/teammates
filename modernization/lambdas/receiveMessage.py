import json
import boto3
from datetime import datetime

def lambda_handler(event, context):
    print("message",event["msg"])

    current_msg = event["msg"]

    sqs = boto3.client('sqs')  #client is required to interact with
    sqs.send_message(
        QueueUrl="https://sqs.us-east-2.amazonaws.com/756766393772/email-queue",
        MessageBody=current_msg
    )

    return {
         'statusCode': 200,
         'body': json.dumps(current_msg)
     }