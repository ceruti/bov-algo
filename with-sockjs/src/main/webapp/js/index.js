import React from "react";
import ReactDom from "react-dom";
import SockJsClient from "react-stomp";
import Fetch from "json-fetch";

class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      clientConnected: false,
      messages: []
    };
  }

  onMessageReceive = (msg, topic) => {
    this.setState(prevState => ({
      messages: [...prevState.messages, msg]
    }));
  }

  sendMessage = (msg, selfMsg) => {
    console.log('sendMessage()...')
    try {
      let selfMsg1 = JSON.stringify(selfMsg);
      console.log(selfMsg1);
      this.clientRef.sendMessage("/app/all", selfMsg1);
      return true;
    } catch(e) {
      return false;
    }
  }

  componentWillMount() {
    Fetch("/history", {
      method: "GET"
    }).then((response) => {
      this.setState({ messages: response.body });
    });
  }

  render() {
    const wsSourceUrl =  "http://localhost:8080/handler";
    return (
      <div>
        <h1>Bov</h1>
        {
          <div>
            {this.state.messages.map(message => {
              return <p>{message.message}</p>
            })}
          </div>
        }

        <SockJsClient url={ wsSourceUrl } topics={["/topic/all"]}
          onMessage={ this.onMessageReceive } ref={ (client) => { this.clientRef = client }}
          onConnect={ () => { this.setState({ clientConnected: true }) } }
          onDisconnect={ () => { this.setState({ clientConnected: false }) } }
          debug={ false }/>
      </div>
    );
  }
}

ReactDom.render(<App />, document.getElementById("root"));
