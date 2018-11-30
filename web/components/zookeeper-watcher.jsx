import React from 'react';
import { action, observable } from 'mobx';
import { observer } from 'mobx-react';
import Tab from '@material-ui/core/Tab/Tab';
import style from './zookeeper-watcher.scss';

class PathData {
  @observable dirty = false;
  @observable data = [];
  
  @action.bound
  setDirty = () => this.dirty = true;
  @action.bound
  setClean = () => this.dirty = false;
  
}


const WatcherHeader = observer(({ data, path, setIndex }) => (
  <Tab label={data.dirty ? `${path} *` : path}
       onClick={setIndex}
  />
));

@observer
class ZookeeperWatcher extends React.Component {
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
