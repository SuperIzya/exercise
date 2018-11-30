import React from 'react';
import { action, observable } from 'mobx';
import { observer } from 'mobx-react';
import Tab from '@material-ui/core/Tab/Tab';
import style from './zookeeper-watcher.scss';
import socket from '../utils/socket';
import { filter, map } from 'rxjs/operators';
import moment from 'moment';

const toTime = ts => moment(ts).format('D/M/YYYY H:mm:ss.SSS');

class PathData {
  @observable dirty = false;
  @observable log = [];
  
  @action.bound
  setDirty = () => this.dirty = true;
  @action.bound
  setClean = () => this.dirty = false;
  
  add = d => this.log.push(d);
}

const WatcherHeader = observer(({ data, path, setIndex }) => (
  <Tab label={data.dirty ? `${path} *` : path}
       onClick={setIndex}
  />
));

const LogLine = ({ line }) => (
  <React.Fragment>
    <div className={style.timestamp}>[{toTime(line.time)}]</div>
    <div className={style.log}>{line.event}</div>
  </React.Fragment>
);

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
    .subscribe(m => data.add({ event: m, time: Date.now() }))
  }
  
  render() {
    const { data, path } = this.props;
    return (
      <div className={style.tabContainer}>
        <div className={style.scrollerContainer}>
          <div className={style.scroller}>
            <div className={style.logs}>
              {data.log.map((log, i) => <LogLine line={log} key={i}/>)}
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export {
  ZookeeperWatcher,
  WatcherHeader,
  PathData
};
