import React from 'react';
import { observer } from 'mobx-react';
import { observable, action } from 'mobx';
import style from './watchers.scss';
import { ZookeeperWatcher, WatcherHeader, PathData } from './zookeeper-watcher';
import Tabs from '@material-ui/core/Tabs/Tabs';
import AppBar from '@material-ui/core/AppBar/AppBar';
import SwipeableViews from 'react-swipeable-views';
import PathForm from './path-form';

class WatchersList {
  @observable watchers = [];
  @observable enterPath = false;
  @observable selectedIndex = 0;
  watchData = {};
  
  @action.bound
  setIndex = index => {
    this.selectedIndex = index;
    this.watchers.map((w, i) => this.watchData[w].setActive(this.selectedIndex === i));
  };
  
  @action.bound
  addWatcher = path => {
    this.watchers.push(path);
    this.watchData[path] = new PathData();
    this.setIndex(this.watchers.length - 1);
  };
  
  @action.bound
  toggleFlag = () => this.enterPath = !this.enterPath;
  
}

const headers = list => (path, i) => <WatcherHeader key={i}
                                                    setActive={() => list.setIndex(i)}
                                                    data={list.watchData[path]}
                                                    path={path}/>;

const content = list => (path, i) => <ZookeeperWatcher key={i}
                                                       path={path}
                                                       data={list.watchData[path]}/>;
  

const WatcherTabs = observer(({ list, indexSetter }) => {
  if (!list.watchers.length) return null;
  return (
    <div className={style.listContainer}>
      <div className={style.list}>
        <AppBar position={'static'} color={'default'}>
          <Tabs
            fullWidth={true}
            value={list.selectedIndex}
            onChange={indexSetter}
            indicatorColor="primary"
            textColor="primary">
            {list.watchers.map(headers(list))}
          </Tabs>
        </AppBar>
        <SwipeableViews axis={'x'}
                        index={list.selectedIndex}
                        onChangeIndex={list.setIndex}>
          {list.watchers.map(content(list))}
        </SwipeableViews>
      </div>
    </div>
  );
});

@observer
class Watchers extends React.Component {
  
  onPathChange = list => event => {
    if (event.target.value) {
      list.toggleFlag();
      const path = event.target.value;
      const index = list.watchers.indexOf(path);
      if (index < 0)
        list.addWatcher(path);
      else list.setIndex(index);
    }
  };
  
  
  setIndex = (evt, index) => this.props.list.setIndex(index);
  
  render() {
    const list = this.props.list;
    return (
      <div className={style.container}>
        <div className={style.form}>
          <PathForm onPathEntered={this.onPathChange(list)}
                    enterPathFlag={list.enterPath}
                    onToggleFlag={list.toggleFlag}/>
        </div>
        <WatcherTabs list={list} indexSetter={this.setIndex}/>
      </div>
    )
  }
}

export {
  Watchers,
  WatchersList
}
