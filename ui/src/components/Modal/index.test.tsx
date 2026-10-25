import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import userEvent from '@testing-library/user-event';
import Modal from '.';
import { IntlWrapper } from 'components/utils/wrapper';

it('Modal snapshot', async () => {
  await (async () => {
    const { baseElement, rerender } = render(
      <Modal open={true}>
        <div>modal content</div>
      </Modal>,
      {
        wrapper: IntlWrapper,
      }
    );
    expect(baseElement).toMatchSnapshot();

    rerender(
      <Modal open={true} footer={<div>footer</div>}>
        <div>modal content</div>
      </Modal>
    );
  })();
});

it('click Modal button', async () => {
  await (async () => {
    const mockHandleCancel = vi.fn();
    const mockHandleConfirm = vi.fn();
    render(
      <Modal open={true} handleCancel={mockHandleCancel} handleConfirm={mockHandleConfirm}>
        <div>modal content</div>
      </Modal>,
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
