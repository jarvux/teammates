import os
import boto3
from botocore.exceptions import ClientError
import json
from datetime import datetime

from_email = "jarvi.zambrano@yopmail.com"
config_set_name = 'teammate-config-name'
client = boto3.client('ses')


def lambda_handler(event, context):
    try:
        bodyStr = event['Records'][0]['body']
        body = json.loads(bodyStr)
        subject = body['subject']
        recipient = body['recipient']
        body_html = body['content']

        email_message = {
            'Body': {
                'Html': {
                    'Charset': 'utf-8',
                    'Data': body_html,
                },
            },
            'Subject': {
                'Charset': 'utf-8',
                'Data': subject,
            },
        }

        ses_response = client.send_email(
            Destination={
                'ToAddresses': [recipient],
            },
            Message=email_message,
            Source=from_email,
            ConfigurationSetName=config_set_name,
        )

    except ClientError as e:
        print(e.response['Error']['Message'])
    else:
        print(ses_response['MessageId'])

    return {
        'statusCode': 200,
        'body': json.dumps('Message sent!')
    }
