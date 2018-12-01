import React from 'react';
import style from './app.scss';
import { Watchers, WatchersList } from './components/watchers';
import Header from './components/header';
import Control from './components/control';

const list = new WatchersList();

const App = () => (
  <div className={style.container}>
    <div className={style.header}>
      <Header/>
    </div>
    <div className={style.content}>
      <div className={style.control}>
        <Control/>
      </div>
      <div className={style.watchers}>
        <Watchers list={list}/>
      </div>
    </div>
  </div>
);

export default App;
