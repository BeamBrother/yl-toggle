import asset_no_data_png from 'images/no-data.png';
import { ReactNode } from 'react';
import { FormattedMessage } from 'react-intl';
import styles from './index.module.scss';

interface IProps {
  children?: import('react').ReactNode;
  text?: string | ReactNode;
}

const NoData = (props: IProps) => {
  const { text } = props;

  return (
    <div className={styles['no-data']}>
      <div>
        <img className={styles['no-data-image']} src={asset_no_data_png} alt='no-data' />
      </div>
      <div>
        { text || <FormattedMessage id='common.nodata.text' />  }
      </div>
    </div>
  );
};

export default NoData;
