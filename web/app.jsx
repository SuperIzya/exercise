import React from 'react';
import ZookeeperWatcher from './components/zookeeper-watcher';
import style from './app.scss';

const App = () => (
  <div className={style.container}>
    <ZookeeperWatcher/>
  </div>
);

export default App;
