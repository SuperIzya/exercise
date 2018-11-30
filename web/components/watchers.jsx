import React from 'react';
import { observer } from 'mobx-react';
import { observable, action } from 'mobx';
import style from './watchers.scss';
import { ZookeeperWatcher, WatcherHeader, PathData } from './zookeeper-watcher';
import Tabs from '@material-ui/core/Tabs/Tabs';
import AppBar from '@material-ui/core/AppBar/AppBar';
import SwipeableViews from 'react-swipeable-views';
import NewPathForm from './path-form';

class WatchersList {
  @observable watchers = ['/a', '/b'];
  @observable enterPath = false;
  @observable selectedIndex = 0;
  watchData = {
    '/a': new PathData(),
    '/b': new PathData()
  };
  
  @action.bound
  setIndex = index => this.selectedIndex = index;
  
  @action.bound
  addWatcher = path => {
    this.watchers.push(path);
    this.watchData[path] = new PathData();
    this.selectedIndex = this.watchers.length - 1;
  };
  
  @action.bound
  toggleFlag = () => this.enterPath = !this.enterPath;
  
}

const headers = list => (path, i) => <WatcherHeader key={i}
                                                    setIndex={() => list.setIndex(i)}
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
    if (event.nativeEvent && event.nativeEvent.keyCode === 13 && event.target.value) {
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
          <NewPathForm onPathEntered={this.onPathChange(list)}
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
