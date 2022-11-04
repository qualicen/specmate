import sys, os
sys.path.insert(0, os.path.expanduser('~/.local/lib/python3.6/site-packages'))
sys.path.insert(0, './util/scripts')

import transformers
from transformers import BertModel, BertTokenizer, get_linear_schedule_with_warmup
import torch

import numpy as np
#from matplotlib import rc

from torch import nn
from torch.utils.data import DataLoader
import torch.nn.functional as F

import json
import os

RANDOM_SEED = 42
np.random.seed(RANDOM_SEED)
torch.manual_seed(RANDOM_SEED)

device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")

class CausalClassifier(nn.Module):
    def __init__(self, n_classes):
        super(CausalClassifier, self).__init__()
        self.bert = BertModel.from_pretrained(PRE_TRAINED_MODEL_NAME)
        self.drop = nn.Dropout(p=0.3)
        self.out = nn.Linear(self.bert.config.hidden_size, n_classes)

    def forward(self, input_ids, attention_mask):
        _, pooled_output = self.bert(input_ids=input_ids, attention_mask=attention_mask, return_dict=False)
        output = self.drop(pooled_output)
        return self.out(output)

class_names = ['not causal', 'causal']
PRE_TRAINED_MODEL_NAME = 'bert-base-cased'

tokenizer = BertTokenizer.from_pretrained(PRE_TRAINED_MODEL_NAME)

model = CausalClassifier(len(class_names))
#path = F"bertclassifier.bin"
path = os.path.join(os.path.abspath(os.path.dirname(__file__)),"bin/cira-classifier.bin")


if torch.cuda.is_available():
    model.load_state_dict(torch.load(path))
else:
    model.load_state_dict(torch.load(path, map_location='cpu'))

model = model.to(device)

def classify(sentence):
    # encode text
    encoded_text = tokenizer.encode_plus(
        sentence,
        max_length=128,
        add_special_tokens=True,
        return_token_type_ids=False,
        pad_to_max_length=True,
        return_attention_mask=True,
        return_tensors='pt',
        truncation=True
    )

    input_ids = encoded_text['input_ids'].to(device)
    attention_mask = encoded_text['attention_mask'].to(device)

    output = model(input_ids, attention_mask)

    _, prediction = torch.max(output, dim=1)
    probs = F.softmax(output, dim=1)

    # build the classification object
    classification = {}
    classification["causal"] = (class_names[prediction] == "causal")
    
    confidence,_ = torch.max(probs, dim=1)
    classification["confidence"] = confidence.item()

    return classification

if __name__ == "__main__":
    sentence = ""
    if len(sys.argv) > 0:
        sentence = sys.argv[1]
    classification = classify(sentence)

    # cast the object to JSON and output it
    print(json.dumps(classification))
    sys.stdout.flush()