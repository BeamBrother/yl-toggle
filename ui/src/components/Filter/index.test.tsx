import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import userEvent from '@testing-library/user-event';
import Filter from '.';
import sleep from 'utils/sleep';
import { IntlWrapper } from 'components/utils/wrapper';

it('Filter snapshot', async () => {
  await (async () => {
    const { asFragment } = render(
      <Filter handleConfirm={vi.fn()} handleClear={vi.fn()} selected>
        <div>
          <div>1</div>
          <div>2</div>
        </div>
      </Filter>
    );
    expect(asFragment()).toMatchSnapshot();
  })();
});

it('Filter icon click snapshot', async () => {
  await (async () => {
    const { baseElement } = render(
      <Filter handleConfirm={vi.fn()} handleClear={vi.fn()} selected={true}>
        <div>
          <div>1</div>
          <div>2</div>
        </div>
      </Filter>
    , { wrapper: IntlWrapper});

    const ele = document.getElementsByClassName('icon-filter')[0];
    await userEvent.click(ele);

    expect(baseElement).toMatchSnapshot();
  })();
});

it('Filter Confirm Button click snapshot', async () => {
  await (async () => {
    const mockHandleCancel = vi.fn();
    const mockHandleConfirm = vi.fn();

    const { baseElement } = render(
      <Filter handleConfirm={mockHandleConfirm} handleClear={mockHandleCancel} selected={true}>
        <div>
          <div>1</div>
          <div>2</div>
        </div>
      </Filter>
    , { wrapper: IntlWrapper});

    const ele = document.getElementsByClassName('icon-filter')[0];
    await userEvent.click(ele);
    expect(baseElement).toMatchSnapshot();

    await sleep(1000);

    await userEvent.click(screen.getByText('Clear'));
    expect(mockHandleCancel).toBeCalled();

    await userEvent.click(screen.getByText('Confirm'));
    expect(mockHandleConfirm).toBeCalled();

    expect(baseElement).toMatchSnapshot();
  })();
});
