import React from 'react';
import { observable, action } from 'mobx';
import style from './control.scss';
import { observer } from 'mobx-react';
import Explore from '@material-ui/icons/Explore';
import ExploreOff from '@material-ui/icons/ExploreOff';
import Fab from '@material-ui/core/Fab/Fab';
import TextField from '@material-ui/core/TextField/TextField';
import Zoom from '@material-ui/core/Zoom/Zoom';
import Send from '@material-ui/icons/Send';
import client from '../utils/client';

class ButtonState {
  @observable disabled = true;
}

const SendButton = observer(({data, send}) => (
  <Fab color="default"
       disabled={data.disabled}
       aria-label="Close"
       onMouseUp={send}>
    <Send/>
  </Fab>
));


class ControlState {
  @observable show = false;
  
  @action.bound
  showControl = () => this.show = true;
  
  @action.bound
  hideControl = () => this.show = false;
}


class Form extends React.Component {
  state = {
    path: '',
    data: ''
  };
  
  buttonState = new ButtonState();
  
  onBlur = field => event => {
    if (event.target.value)
      this.setState({ [field]: event.target.value });
  };
  send = () => this.props.send(this.state);
  
  onChange = other => event => {
    this.buttonState.disabled = !this.state[other] || !event.target.value;
  };
  
  render() {
    return (
      <div className={style.fields}>
        <TextField label={'z-node'}
                   placeholder={'Enter z-node path'}
                   defaultValue={this.state.path}
                   onChange={this.onChange('data')}
                   onBlur={this.onBlur('path')}/>
        <TextField label={'data'}
                   defaultValue={this.state.data}
                   onChange={this.onChange('path')}
                   onBlur={this.onBlur('data')}
                   placeholder={'Enter data to write to the node'}/>
        <div className={style.send}>
          <SendButton data={this.buttonState} send={this.send}/>
        </div>
      </div>
    )
  }
}

const send = state => data => {
  client.post('api/write', data);
  state.hideControl();
};

const ControlFields = observer(({ state }) => (
  <div className={style.form}>
    <div className={style.button}>
      <Fab color="secondary" aria-label="Close" onMouseUp={state.hideControl}>
        <ExploreOff/>
      </Fab>
    </div>
    <Zoom in={true}>
      <Form send={send(state)}/>
    </Zoom>
  </div>
));

const ControlForm = observer(({ state }) => {
  if (!state.show) {
    return (
      <Fab color="primary" aria-label="Open" onMouseUp={state.showControl}>
        <Explore/>
      </Fab>
    );
  }
  return <ControlFields state={state}/>
});

const Control = () => (
  <div className={style.container}>
    <ControlForm state={new ControlState()}/>
  </div>
);

export default Control;
