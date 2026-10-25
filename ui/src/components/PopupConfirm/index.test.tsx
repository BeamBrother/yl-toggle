import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import userEvent from '@testing-library/user-event';
import PopupConFirm from '.';
import { IntlWrapper } from 'components/utils/wrapper';

it('PopupConFirm snapshot', async () => {
  await (async () => {
    const { baseElement } = render(
      <PopupConFirm open={true} text={'popup confirm text'} handleCancel={vi.fn()} handleConfirm={vi.fn()}>
        <span>this is test text.</span>
      </PopupConFirm>,
      {
        wrapper: IntlWrapper,
      }
    );
    expect(baseElement).toMatchSnapshot();
  })();
});

it('click PopupConFirm button', async () => {
  await (async () => {
    const mockHandleCancel = vi.fn();
    const mockHandleConfirm = vi.fn();
    render(
      <PopupConFirm
        open={true}
        text={'popup confirm text'}
        handleCancel={mockHandleCancel}
        handleConfirm={mockHandleConfirm}
      >
        <span>this is test text.</span>
      </PopupConFirm>,
      {
        wrapper: IntlWrapper,
      }
    );

    await userEvent.click(screen.getByText('Cancel'));
    await userEvent.click(screen.getByText('Confirm'));

    expect(mockHandleCancel).toBeCalled();
    expect(mockHandleConfirm).toBeCalled();
  })();
});
