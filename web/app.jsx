import React from 'react';
import ZookeeperWatcher from './components/zookeeper-watcher';
import style from './app.scss';
import DevTools from "mobx-react-devtools";


const App = () => (
  <div className={style.container}>
    <ZookeeperWatcher/>
    <DevTools/>
  </div>
);

export default App;
