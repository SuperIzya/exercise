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
  
  onEntered = field => event => this.setState({ [field]: this.state[field] + event.key });
  
  renderButton() {
    const isDisabled = !this.state.path || !this.state.data;
    return (
      <Fab color="default"
           disabled={isDisabled}
           aria-label="Close"
           onMouseUp={() => this.props.send(this.state)}>
        <Send/>
      </Fab>
    )
  }
  
  render() {
    return (
      <div className={style.fields}>
        <TextField label={'z-node'}
                   placeholder={'Enter z-node path'}
                   value={this.state.path}
                   onKeyPress={this.onEntered('path')}/>
        <TextField label={'data'}
                   value={this.state.data}
                   onKeyPress={this.onEntered('data')}
                   placeholder={'Enter data to write to the node'}/>
        {this.renderButton()}
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
