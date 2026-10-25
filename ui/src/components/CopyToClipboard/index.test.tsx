import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import userEvent from '@testing-library/user-event';
import { act } from 'react-dom/test-utils';
import { IntlWrapper } from 'components/utils/wrapper';
import CopyToClipboard from '.';
import sleep from 'utils/sleep';

window.prompt = vi.fn();

it('CopyToClipboard snapshot', async () => {
  await (async () => {
    const { asFragment } = render(
      <CopyToClipboard text="this is a test text">
        <span>this is a test text</span>
      </CopyToClipboard>
    );
    await sleep(3000);
    expect(asFragment()).toMatchSnapshot();
  })();
});

it('CopyToClipboard hover snapshot', async () => {
  await (async () => {
    const { baseElement } = render(
      <CopyToClipboard text="this is a test text">
        <span>this is a test text</span>
      </CopyToClipboard>,
      {
        wrapper: IntlWrapper,
      }
    );
    await act(async () => {
      const ele = screen.getByText('this is a test text');
      await userEvent.hover(ele);
      await sleep(1000);
    });

    expect(baseElement).toMatchSnapshot();

    await act(async () => {
      const ele = screen.getByText('this is a test text');
      await userEvent.unhover(ele);
      await sleep(1000);
    });

    expect(baseElement).toMatchSnapshot();
  })();
});

console.error = vi.fn();

it('Click CopyToClipboard', async () => {
  vi.setConfig({ testTimeout: 10000 });
  await (async () => {
    const { baseElement } = render(
      <CopyToClipboard text="this is a test text">
        <span>this is a test text</span>
      </CopyToClipboard>,
      {
        wrapper: IntlWrapper,
      }
    );

    await act(async () => {
      const ele = screen.getByText('this is a test text');
      await userEvent.click(ele);
      await userEvent.hover(ele);
      await sleep(500);
      expect(baseElement).toMatchSnapshot();
      await sleep(2000);
      expect(baseElement).toMatchSnapshot();
    });
  })();
});
