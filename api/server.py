from flask import Flask
import json

app = Flask(__name__)


@app.route("/")
def home():
    with open("sample_data.json") as f:
        return json.load(f)


if __name__ == "__main__":
    app.run()
