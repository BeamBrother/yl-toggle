import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import dayjs from 'dayjs';
import { ToastContainer } from 'react-toastify';
import Router from './router';
import { I18NContainer } from './hooks';
import relativeTime from 'dayjs/plugin/relativeTime';
import updateLocale from 'dayjs/plugin/updateLocale';
import Intl from './locales/intl';
import 'iconfont/iconfont.css';
import 'semantic-ui-less/semantic.less';
import 'react-datetime/css/react-datetime.css';
import 'react-toastify/dist/ReactToastify.css';
import 'dayjs/locale/zh-cn';
import './index.scss';
import './global.scss';

dayjs.extend(relativeTime);
dayjs.extend(updateLocale);

const container = document.getElementById('root');
if (!container) {
  throw new Error('Root container is missing');
}

createRoot(container).render(
  <StrictMode>
    <I18NContainer.Provider>
      <Intl>
        <Router />
      </Intl>
      <ToastContainer theme='colored' />
    </I18NContainer.Provider>
  </StrictMode>
);
