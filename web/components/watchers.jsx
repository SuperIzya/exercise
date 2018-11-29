import React from 'react';
import { observer } from 'mobx-react';
import { observable, action } from 'mobx';
import style from './watchers.scss';
import ZookeeperWatcher from './zookeeper-watcher';
import Fab from '@material-ui/core/Fab/Fab';
import AddIcon from '@material-ui/icons/Add';
import HighlightOff from '@material-ui/icons/HighlightOff';
import Zoom from '@material-ui/core/Zoom';

class WatchersList {
  @observable watchers = [];
  
  @observable enterPath = false;
  
  @action.bound
  addWatcher = path => this.watchers.push(path);
  
  @action.bound
  toggleFlag = () => this.enterPath = !this.enterPath;
  
}

@observer
class Watchers extends React.PureComponent {
  renderNewButton = list => (
    <Fab color="primary" aria-label="Add" className={classes.fab} onMouseUp={list.toggleFlag}>
      <AddIcon/>
    </Fab>
  );
  
  renderCloseButton = list => (
    <Fab color="secondary" aria-label="Close" className={classes.fab} onMouseUp={list.toggleFlag}>
      <HighlightOff/>
    </Fab>
  );
  
  renderForm = list => (
    <Zoom in={list.enterPath}>
      <div className={style.form}>
        <label htmlFor={"watch-path"}>Path to watch</label>
        <input type={'text'} ref={this.text}/>
      </div>
    </Zoom>
  );
  
  render() {
    const list = this.props.list;
    return (
      <div className={style.container}>
        <div className={style.button}>
          {list.enterPath ? this.renderCloseButton(list) : this.renderNewButton(list)}
        </div>
        {this.renderForm(list)}
        <div className={style.list}>
          {list.watchers.map((w, i) => <ZookeeperWatcher key={i} path={w}/>)}
        </div>
      </div>
    )
  }
}