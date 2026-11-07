import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import userEvent from '@testing-library/user-event';
import SwitchLanguage from '.';
import I18nWrapper from 'components/utils/containerWrapper';
import sleep from 'utils/sleep';
import { act } from 'react-dom/test-utils';

it('Switch language snapshot', async () => {
  await (async () => {
    const { asFragment } = render(
      <SwitchLanguage />, { wrapper: I18nWrapper}
    );

    expect(asFragment()).toMatchSnapshot();
  })();
});


it('click language', async () => {
  await (async () => {
    const { baseElement } = render(
      <SwitchLanguage />, {  wrapper: I18nWrapper, }
    );

    await act(async () => {
      const ele = screen.getByText('English');
      await userEvent.click(ele);
      await sleep(500);
    });

    expect(baseElement).toMatchSnapshot();
  })();
});

it('select language', async () => {
  await (async () => {
    const { baseElement } = render(
      <SwitchLanguage />, {  wrapper: I18nWrapper, }
    );

    await act(async () => {
      const ele = screen.getByText('English');
      await userEvent.click(ele);
      await sleep(500);
      const eleZh = screen.getByText('中文');
      await userEvent.click(eleZh);
      await sleep(500);
    });

    expect(baseElement).toMatchSnapshot();
  })();
});
