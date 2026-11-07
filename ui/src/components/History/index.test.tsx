import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import userEvent from '@testing-library/user-event';
import { IntlWrapper } from 'components/utils/wrapper';
import History from '.';
import segmentResponse from './mockData/segmentResponse.json';
import targetResponse from './mockData/targetResponse.json';
import sleep from 'utils/sleep';

it('History snapshot', async () => {
  await (async () => {
    const { asFragment, rerender } = render(
      <History
        versions={[]}
        hasMore={true}
        latestVersion={0}
        loadMore={vi.fn()}
        selectedVersion={0}
        isHistoryLoading={false}
        reviewHistory={vi.fn()}
      />,
      {
        wrapper: IntlWrapper,
      }
    );

    expect(asFragment()).toMatchSnapshot();

    rerender(<History
      versions={[]}
      hasMore={true}
      latestVersion={0}
      loadMore={vi.fn()}
      selectedVersion={0}
      isHistoryLoading={true}
      reviewHistory={vi.fn()}
    />);

    expect(asFragment()).toMatchSnapshot();
  })();
});

it('History render segment data', async () => {
  await (async () => {
    const load = vi.fn().mockResolvedValue(segmentResponse);

    const mock = vi.fn();
    const { asFragment, rerender } = render(
      <History
        versions={segmentResponse.content}
        hasMore={true}
        latestVersion={segmentResponse.content[0].version}
        loadMore={load}
        selectedVersion={0}
        isHistoryLoading={false}
        reviewHistory={mock}
      />,
      {
        wrapper: IntlWrapper,
      }
    );

    expect(asFragment()).toMatchSnapshot();

    await userEvent.click(screen.getByText('Load more'));
    await sleep(0);

    expect(asFragment()).toMatchSnapshot();

    await userEvent.click(screen.getByText('7'));
    expect(mock).toBeCalledWith(expect.objectContaining({ version: 7 }));

    rerender(
      <History
        versions={segmentResponse.content}
        hasMore={true}
        latestVersion={segmentResponse.content[0].version}
        loadMore={load}
        selectedVersion={7}
        isHistoryLoading={false}
        reviewHistory={mock}
      />
    );
    await userEvent.click(screen.getByText('7'));
    expect(mock).toBeCalledTimes(1);
  })();
});

it('History render target data', async () => {
  await (async () => {
    const load = vi.fn().mockResolvedValue(targetResponse);

    const mock = vi.fn();
    const { asFragment } = render(
      <History
        versions={targetResponse.content}
        hasMore={true}
        latestVersion={targetResponse.content[0].version}
        loadMore={load}
        selectedVersion={0}
        isHistoryLoading={false}
        reviewHistory={mock}
      />,
      {
        wrapper: IntlWrapper,
      }
    );

    expect(asFragment()).toMatchSnapshot();
  })();
});
