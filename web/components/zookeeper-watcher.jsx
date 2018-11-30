import React from 'react';
import { action, observable } from 'mobx';
import { observer } from 'mobx-react';
import Tab from '@material-ui/core/Tab/Tab';
import style from './zookeeper-watcher.scss';
import socket from '../utils/socket';
import { filter, map } from 'rxjs/operators';

class PathData {
  @observable dirty = false;
  @observable data = [];
  
  @action.bound
  setDirty = () => this.dirty = true;
  @action.bound
  setClean = () => this.dirty = false;
  
  add = d => this.data.push(d);
}

const WatcherHeader = observer(({ data, path, setIndex }) => (
  <Tab label={data.dirty ? `${path} *` : path}
       onClick={setIndex}
  />
));

@observer
class ZookeeperWatcher extends React.Component {
  constructor(props) {
    super(props);
    const { data, path } = this.props;
    const prefix = `${path}: `;
    socket.send(path);
    socket.messages.pipe(
      filter(m => m.startsWith(prefix)),
      map(m => m.substr(prefix.length))
    )
    .subscribe(m => data.add(m))
  }
  
  render() {
    const { data, path } = this.props;
    return (
      <div className={style.tabContainer}>{path}</div>
    );
  }
}

export {
  ZookeeperWatcher,
  WatcherHeader,
  PathData
};
