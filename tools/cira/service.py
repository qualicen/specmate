import argparse
import fastapi
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from hashlib import sha3_256
import uvicorn
from typing import List, Dict

import classifier
import multilabeler as labeler


cira_api = FastAPI()
cira_api.cache = dict()

class SentenceRequest(BaseModel):
    sentence: str
    language: str = "en"

class ClassificationResponse(BaseModel):
    causal: bool
    confidence: float

class LabelsResponse(BaseModel):
    id: str
    sentence: str
    labels: List[Dict]

# heartbeat request
@cira_api.get('/api')
def read_api_version():
    return {'version': 'v1'}

# classify a given sentence
@cira_api.post('/api/classify', response_model=ClassificationResponse)
async def create_classification(req: SentenceRequest):
    classification = classifier.classify(req.sentence)
    response = ClassificationResponse(
        causal = classification['causal'], 
        confidence = classification['confidence'])
    return response

# label a given sentence
@cira_api.post('/api/label', response_model=LabelsResponse)
async def create_classification(req: SentenceRequest):
    req_id = sha3_256(req.sentence.encode('utf-8')).hexdigest()
    if not req_id in cira_api.cache:
        labels = labeler.label(req.sentence)
        cira_api.cache[req_id] = LabelsResponse(
            id=req_id, 
            sentence=req.sentence, 
            labels=labels)
    return cira_api.cache[req_id]

# returns a specific sentence if it exists
@cira_api.get('/api/sentence/{cr_id}')
def classification_request(cr_id: str):
    if cr_id not in cira_api.cache:
        raise HTTPException(
            status_code=404, detail='Label request {} not found'.format(cr_id))
    else:
        return cira_api.cache[cr_id]

# returns the number of existing labeled sentences from the cache
@cira_api.get('/api/labels')
def get_labels():
    return {'number_of_labels': len(cira_api.cache)}

# delete the cache
@cira_api.delete('/api/cache')
def delete_classification():
    cira_api.cache = {}

# main loop
if __name__ == '__main__':
    # parse arguments
    parser = argparse.ArgumentParser()
    parser.add_argument('--url', help='URL to be used to host the service', type=str, default='172.19.0.1')
    parser.add_argument('--port', help='Port to be used on the localhost for the service', type=int, default=8042)
    args = parser.parse_args()

    # start the service
    uvicorn.run(cira_api, host=args.url, port=args.port)
